var request = require('request');
var rf = require('fs');
var https = require('https');
var data = rf.readFileSync("createcredentials.groovy", "utf-8");
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
    var config = require('./config.json');
    var finalurl = 'http://' + config['administrator'][0]['username'] + ':' + config['administrator'][0]['password'] + '@' + config['url'].substr(7) + 'scriptText';
    console.log(finalurl);
    request.post(
        {
            url : finalurl,
            form:
            {
                //user : 'admin:admin',
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