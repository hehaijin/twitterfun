A demo of Play Framework 2.6.x connecting to the Twitter streaming API

It's written when reading the book "Reactive Web Applications" by Manuel Bernhardt, and some code is from the book.

The project is using the Akka Streams API rather then the iteratee/enumerator in the book to consume the twitter stream, and is easier to follow.







To run the project, first add credentials. Add folowing line to application.conf or a credentials.conf under the same folder. 
Add supply the required credentials.

twitter.apiKey=""
twitter.apiSecret=""
twitter.token=""
twitter.tokenSecret=""



Access "localhost:9000/stream/your-topic-here" to listen to twitter steam with any topic of interest.
