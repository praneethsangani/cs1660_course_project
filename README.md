# cs1660_course_project

## Completed all the main requirements other than dockerizing the GUI.

#### I attached one of the many dockerfiles I experimented with. 
#### I also tried the following commands with a lot of other variations. In the end I was unable to get the GUI dockerized.: 
#### socat TCP-LISTEN:6000,reuseaddr,fork UNIX-CLIENT:\"$DISPLAY\"
#### docker run -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=$(ipconfig getifaddr en0):0 myImageName

### The GUI communicates with GCP. Uploades files once you select them and constructs the indicies.

## Video: 
https://drive.google.com/file/d/1ea9Iqw7hnN6PRX2JJuNx7zl2APUqOs4n/view?usp=sharing
