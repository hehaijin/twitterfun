    
	var topic= document.getElementById("topic").innerHTML
	var url="ws://localhost:9000/tweets/"+topic;
	console.log(url)
    var tweetSocket=new WebSocket(url);
    tweetSocket.onmessage= function(event){
        console.log(event.data);
		//var data=event.data;
        var data= JSON.parse(event.data);
        var tweet=document.createElement("p");
        var text=document.createTextNode(data.text);
        tweet.appendChild(text);
        document.getElementById("tweets").appendChild(tweet);
	}
