HOW TO COMPILE OPENCV WITH FFMPEG FOR JAVA:

This here contains all the important information to successfully compile and use opencv in java with ffmpeg support

1. download opencv source code
2. download and install ffmpeg in system
3. download cmake
4. compile opencv in a seperate build folder using cmake
   4.1. make sure java configurations are ok ant and stuff is installed
   4.2. download opencv contrib and set thet as extra modules
   4.3. build with ffmpeg and generate
5. go to visual studio and set the build to release and in cmake targets first build build all and then build install
6. make sure no fails and then set env variables for opencv java from install path in the build folder
7. include that in the project and that should work
8. to access ip u also need to include opencv ffmpeg dll in the project and set that as system path variables
9. dones hopefully!

This is a very rough guide but it kinda documents my very long struggle to compilea single library with an added functionality
hopefully it is useful to someone in the future!
