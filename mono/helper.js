'use strict';
const os = require('os');
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.runCommand = runCommand;

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _child_process = require('child_process');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function runCommand(cwd, cmd, args, callback) {
  // important to have __dirname as current working directory.
  var options = {
    cwd: cwd || __dirname,
    env: process.env
  };
  var escapedArgs = args.map(function (arg) {
    return '"' + arg + '"';
  });
  
  if(os.platform() == "win32") var commandLine = _lodash2.default.flatten([cmd, escapedArgs]).join(' ');
  else var commandLine = _lodash2.default.flatten(["mono", cmd, escapedArgs]).join(' ');
  (0, _child_process.exec)(commandLine, options, function (err, stdout, stderr) {
    callback(err, stdout, stderr);
  });
}