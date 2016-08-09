"use strict";

var fs = require('fs');
var _ = require('lodash');
var exec = require('child_process').exec;
var gulp = require('gulp');
var clean = require('gulp-clean');
var copy = require('gulp-copy');
var postcss = require('gulp-postcss');
var jshint = require('gulp-jshint');
var msbuild = require("gulp-msbuild");
var nugetRestore = require('gulp-nuget-restore');
var gutil = require('gulp-util');
var browserSync = require('browser-sync').create();
var path = require('path');
var runSequence = require('run-sequence');
var webpack = require('webpack');
var WebpackDevServer = require("webpack-dev-server");
var config = require('./webpack.hot.config.js');
var proxyMiddleware = require('http-proxy-middleware');
var replaceInFile = require('gulp-replace');
var buildUhf = require('./tools/buildUhfService');

// path config
var docs = {
    source: 'src',
    dist: 'dist/',
    build: 'build/',
    theme: 'src/themes',
    drop: 'drop',
    temp: 'temp'
};


if (!fs.existsSync('./tools/packages.config')) {
    throw new gutil.PluginError("run_docfx", "Cannot find packages.config under tools folder.");
}
var versionReg = /package\s+id\=\"docfx\.msbuild\"\s+version=\"([^"]+)\"/g;
var versionMatch = versionReg.exec(fs.readFileSync('./tools/packages.config', 'utf-8'));
var version = versionMatch ? versionMatch[1] : '';
if (!version) {
    throw new gutil.PluginError("run_docfx", "Cannot read version for docfx.msbuild.");
}
docs.docfx_exe = 'tools/packages/docfx.msbuild.' + version + '/tools/docfx.exe';

// clean DocPacker output folder and drop/temp/tmp folders.
gulp.task('clean', function() {
    return gulp.src([
        docs.source + "/DocPacker/bin/Release/**/*",
        docs.source + "/DocPacker/obj/Release/**/*",
        docs.drop,
        docs.temp,
        "tmp"], {read: false})
        .pipe(clean({force: true}));
});

// run jshint to javascripts and stop execution when violation occurs.
gulp.task('jshint', function () {
    return gulp.src([
        docs.theme + '/javascript/**/*.js',
        '!' + docs.theme + '/javascript/**/*.min.js',
    ]).pipe(jshint(docs.theme + '/javascript/.jshintrc'))
        .pipe(jshint.reporter('default'))
        .pipe(jshint.reporter('fail'));
});

// this task builds the docpacker.sln at Release configuration
gulp.task('msbuild', function () {
    return gulp.src(docs.source + "/docpacker.sln")
        .pipe(nugetRestore({additionalArgs: ["-PackagesDirectory", docs.source + "\\packages"]}))
        .pipe(msbuild({
                configuration: 'Release',
                targets: ['Clean', 'Rebuild'],
                maxcpucount: 1,
                toolsVersion:14.0,
                WarningLevel: 2,
                properties: {
                    VisualStudioVersion: '14.0',
                    Platform: 'Any CPU'
                },
            })
        );
});

// this is used for mono
gulp.task('msbuild-mono', function () {
    return gulp.src(docs.source + "/docpacker.sln")
        .pipe(nugetRestore({monoPath:"mono", additionalArgs: ["-PackagesDirectory", docs.source + "/packages"]}))
        .pipe(msbuild({
                configuration: 'Release',
                targets: ['Clean', 'Rebuild'],
                maxcpucount: 1,
                toolsVersion:14.0,
                WarningLevel: 2,
                properties: {
                    VisualStudioVersion: '14.0',
                    Platform: 'Any CPU'
                },
            })
        );
});

gulp.task('buildUhf', function() {
    buildUhf(docs.temp + '/_themes/');
});

// this task builds postcss against themes/css, and output the results to temp/_themes/css
gulp.task('postcss', function () {
    var processors = [
        require('msops-css')(),
        require('rucksack-css')({
            autoprefixer: true,
        }),
        require('postcss-cachebuster')
    ];
    return gulp.src(docs.theme + '/css/+(conceptual|hubpage|reference|restapi|contentpage).css')
        .pipe(postcss(processors))
        .pipe(gulp.dest(docs.temp + '/_themes/css/'));
});

gulp.task('removetocext', function () {
    return gulp.src([docs.drop + '/site/TOC.json'], { base: docs.drop })
        .pipe(replaceInFile('.html', ''))
        .pipe(gulp.dest(docs.drop));
});

// this is the help method for exec shell commands.
var runCommand = function(cmd, args, cb) {
    // important to have __dirname as current working directory.
    var options = {
        cwd: __dirname,
        env: process.env
    };
    exec(_.flatten([path.join(__dirname, cmd), args]).join(' '), options, function(err, stdout, stderr) {
        gutil.log("[run_docx]", stdout.toString());
        gutil.log("[run_docx]", stderr.toString());
        cb(err);
    });
};

// this is used for mono
var runCommand_mono = function(cmd, args, cb) {
    // important to have __dirname as current working directory.
    var options = {
        cwd: __dirname,
        env: process.env
    };
    exec(_.flatten([cmd, args]).join(' '), options, function(err, stdout, stderr) {
        gutil.log("[run_docx]", stdout.toString());
        gutil.log("[run_docx]", stderr.toString());
        cb(err);
    });
};

var run_docfx = function(docfx_json, output, cb) {
    runCommand(docs.docfx_exe, [docfx_json, '-o', output], cb)
};

// this is used for mono
var run_docfx_mono = function(docfx_json, output, cb) {
    runCommand_mono("mono", [path.join(__dirname, docs.docfx_exe), docfx_json, '-o', output], cb)
};

// docfx tasks
gulp.task('run:docfx', function(cb) {
    run_docfx('test/data/docfx.json', docs.temp + '/site.docfx', cb);
});

// this is used for mono
gulp.task('run:docfx-mono', function(cb) {
    run_docfx_mono('test/data/docfx.json', docs.temp + '/site.docfx', cb);
});

// docpacker tasks
gulp.task('run:docpacker', function(cb) {
    runCommand(docs.temp + '/DocPacker/DocPacker.exe', ['-r', docs.temp + '/site.docfx', '-o', docs.drop + '/site', '-s'], cb);
});

// this is used for mono
gulp.task('run:docpacker-mono', function(cb) {
    runCommand_mono("mono", [docs.temp + '/DocPacker/DocPacker.exe', '-r', docs.temp + '/site.docfx', '-o', docs.drop + '/site', '-s'], cb);
});

// webpack the javascript at src/themes/javascripts in memory together with hot-load plugin and serve temp/_themes at http://localhost:9001
gulp.task("webpack:dev-server", function(callback) {
    var devServer = new WebpackDevServer(webpack(config), {
        contentBase: path.join(__dirname, 'temp'),
        watch: true,
        keepalive: true,
        inline: true,
        historyApiFallback: true,
        quite: false,
        hot: true,
        watchOptions: {
            aggregateTimeout: 100
        },
        noInfo: true,
        stats: {
            colors: true,
            assets: true,
            version: false,
            hash: false,
            timings: false,
            chunks: false,
            chunkModules: true
        },
        headers: { 'Access-Control-Allow-Origin': '*' },
        publicPath: config.output.publicPath
    });
    devServer.listen(9001, "0.0.0.0", function(err) {
        if (err) {
            throw new gutil.PluginError("webpack-dev-server", err);
        }
        return callback();
    });
});

// when liquid changes, simply update liquid files to temp/site.docfx/_themes for DocPacker.exe to use.
gulp.task('copy:liquid', function() {
    return gulp.src(docs.theme + '/**/*.liquid')
        .pipe(copy(docs.temp + '/site.docfx/_themes', {prefix: 2}));
});

// watch css and liquid files, reload after build task finishes.
gulp.task('watch', ["webpack:dev-server"] ,function() {
    gulp.watch(docs.theme + '/css/**/*.css', ['postcss', browserSync.reload]);
    gulp.watch(docs.theme + '/**/*.liquid', function() {
        runSequence('copy:liquid', 'run:docpacker', function() {
            browserSync.reload();
        });
    });
    // serve at http://localhost:3000 while redirect all request contains /_themes/ to WebpackDevServer
    var proxy = proxyMiddleware('/**/_themes/**/*', {
        target: 'http://localhost:9001',
        pathRewrite: {
            '^/.*/_themes/' : '/_themes/'       // remove path
        }
    });
    browserSync.init({
        server: {
            baseDir: path.join(__dirname, "drop/site/"),
            directory: true,
            middleware: [proxy, function (req, res, next) {
                if (req.url === '' || req.url === '/' || req.url === '/en-us') {
                    res.writeHead(302, { Location: '/en-us/' });
                    res.end();
                    return;
                }
                var cookies = new (require('cookies'))(req, res);
                if (!cookies.get('NinjaMode')) {
                    cookies.set('NinjaMode', 'true', {httpOnly: false, overwrite: true, path: '/'});
                }
                req.url = req.url.replace(/^\/en-us\//, '/');

                // Simulate the no-HTML-extension in the URL ['/en-us/' part has been removed now]
                var reqUrl = require('urijs')(req.url);
                if (reqUrl.segment() && reqUrl.segment().length === 2 && reqUrl.segment()[0] !== '_api') {
                    // Accessing files like '/conceptual/xxx', '/reference/xxx'
                    if (reqUrl.suffix() === 'html') {
                        var redirectedUrlString = '/en-us' + reqUrl.suffix('').toString();
                        console.log('Detected HTML url "' + req.url + '", will redirect to "' + redirectedUrlString + '"');
                        res.writeHead(302, { Location: redirectedUrlString });
                        res.end();
                        return;
                    } else if (reqUrl.filename() && reqUrl.filename().length) {
                        var expectedTargetFile = null;
                        if (!reqUrl.suffix()) {
                            // '/conceptual/Install_ATA'
                            expectedTargetFile = reqUrl.suffix('html').toString();
                        } else if (reqUrl.suffix() !== 'json' && reqUrl.suffix() !== 'png') {
                            // include '/reference/System.String', but exclude '/conceptual/TOC.json'
                            expectedTargetFile = reqUrl.suffix(reqUrl.suffix() + '.html').toString();
                        }
                        if (expectedTargetFile) {
                            console.log('Appending HTML extension to "' + req.url + '", which will load the file "' + expectedTargetFile + '"');
                            req.url = expectedTargetFile;
                        }
                    }
                }

                return next();
            }]
        },
        port: 3000,
        open: false
    });
});

// copy file tasks
gulp.task('copy:themes', function() {
    return gulp.src([docs.theme  + '/**/*', '!' + docs.theme + '/javascript/**/*.js', '!' + docs.theme + '/**/*.css'], {
          dot: true
      }).pipe(copy('temp/_themes', {prefix: 2}));
});

gulp.task('copy:DocPacker', function() {
    return gulp.src(docs.source + '/DocPacker/bin/Release/*.+(exe|dll|config|json)')
        .pipe(copy(docs.temp + '/DocPacker', {prefix: 4}));
});

gulp.task('copy:breadcrumb', function() {
    return gulp.src('test/data/breadcrumb.json')
        .pipe(copy(docs.drop + '/site', {prefix: 2}));
});

// collect copy tasks together
gulp.task('copy:dev', ['copy:breadcrumb', 'copy:DocPacker', 'copy:themes']);

gulp.task('dev', function(cb) {
    runSequence(['clean', 'msbuild', 'jshint'],
        'postcss',
        'copy:dev',
        'buildUhf',
        'run:docfx',
        'run:docpacker',
        'removetocext',
        'watch',
        cb);
});

// when used in mono please call gulp dev-mono
gulp.task('dev-mono', function(cb) {
    runSequence(['clean', 'msbuild-mono', 'jshint'],
        'postcss',
        'copy:dev',
//        'buildUhf',
        'run:docfx-mono',
        'run:docpacker-mono',
        'removetocext',
        'watch',
        cb);
});