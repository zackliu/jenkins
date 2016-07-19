var request = require('request');
var rf = require('fs');
var https = require('https');
var data = rf.readFileSync("createaccounts.groovy", "utf-8");
//console.log(data);

var json = "";
var url = "https://raw.githubusercontent.com/zackliu/jenkins/master/config.json";
https.get(url, function(res) {
    res.on('data', function(data) {
        json += data;
    }).on('end', function() {
        rf.writeFileSync("./config.json", json);
        console.log("config ok");
        doPost();
    });
    
});


function doPost(){
   // var config = JSON.parse('config.json');
    var config = require('./config.json');
    var finalurl = 'http://' + config['administrator'][0]['username'] + ':' + "da1a97c12e0a4a78b09bc1032b655909" + '@' + "sitexpagents.eastasia.cloudapp.azure.com/" + 'scriptText';
    console.log(finalurl);
    request.post(
        {
            url : finalurl,
            form:
            {
                script : data.toString()
            }
        },
        function(error, response, body)
        {
            if(response.statusCode == 200)
            {
                console.log(body);
            }
                
            else
                console.log(response.statusCode);
        }
        
    );
}