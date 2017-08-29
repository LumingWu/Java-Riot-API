# Java-Riot-API

This is a library for Java programmers to make requests to
the riot api while respecting the Riot's rate limit.

I do not promise that I can keep this library up to date because
I am not a developer of a profitable LOL support application.

Riot may change the API and rate limit rule at any time, so it is
your responsibility to check if my library is going to be useful.

However, I am sure this library will at least be a good start
if you are planning to work on a project based on Riot API in
Java, and I am glad if I can help to contribute to the League
of Legends community.

During the development of the library, I learned and wrote a 
rate limiter. You may check it out at https://github.com/LumingWu/rate-limiter

Usage: Create an instance of the Requester and try to store it
in the ServletContext / ApplicationContext because you are going
to use it as long as the server is booting.

Notice that the rate limiter will not record between server 
restarts, if you are going to restart, have the rate limit in
your mind.