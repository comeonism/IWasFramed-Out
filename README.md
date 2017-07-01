# IWasFramed-Out

## What is it? 

A simple tool that, provided with a video file, start/end times and 'step' length (in millis) generates frames from the video. 
Frames start at start time (inclusive) and end at end time (also inclusive). 
If end time is not provided - frames will be generated from start time to the end of the video. 
The frames are separated by the 'step' amount of milliseconds. 
The frames are stored to a local directory and afterwards uploaded to AWS S3. 
    
To make it work for your S3 bucket make sure to provide a proper bucket name in Utils.
    
     
## Why 'I Was Framed - Out'
    
'I Was Framed' because I love awful puns and this tool extracts frames. 'Out' because there's also going to be an 'In'. 
    
    
## Technologies used

- JavaFX for GUI stuff
- Xuggler for video processing
- Amazon AWS stuff for uploading to... **gasp** AWS S3

