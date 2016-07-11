var https = require('https');
var fs = require('fs')

var json = "";
var url = "https://raw.githubusercontent.com/zackliu/jenkins/master/config.json";
https.get(url, function(res) {
    res.on('data', function(data) {
        json += data;
    }).on('end', function() {
        fs.writeFile("./config.json", json);
    });

    console.log("over");
});