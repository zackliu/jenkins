var request = require('request');
var rf = require('fs');
var https = require('https');
var data = rf.readFileSync("createjobs.groovy", "utf-8");
//console.log(data);

var times = 100;

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
            if(response != null &&  response.statusCode == 200)
            {
                console.log(body);
            }
                
            else if (response == null || response.statusCode == 503)
            {
                if(response != null && times <= 0) console.log(response.statusCode);
                else
                {
                    console.log("Wait........");
                    times = times - 1;
                    setTimeout(doPost, 5000);
                }
            }
            else
            {
                console.log(response.statusCode);
            }
                
        }
        
    );

}