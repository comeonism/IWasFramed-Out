# IWasFramed-Out

## What is it? 

I wanted a tool for my tracing table. The idea was - I project images onto the glass surface of my table and trace them (pen and tracing paper). Sometimes I don't want images to be projected, though, I just want a backlight. 'I Was Framed' is made for both of those things.

'I Was Framed - Out' is a sibling of [this gal over here](https://github.com/comeonism/IWasFramed-In) (I Was Framed - In). 

Basically, 'I Was Framed - Out' deals with everything but tracing things. It's the stuff you do beforehand, possibly from a different computer or something.

'I Was Framed - Out' is a simple tool that can be summarised by these points: 
- Given a video file, start/end times and 'step' length (in millis) extracts frames from the video. 
- Frames start at start time (inclusive) and end at end time (also inclusive). 
- If end time is not provided - frames will be generated from start time to the end of the video. 
- The frames are separated by the 'step' amount of milliseconds. 
- The frames are stored to a local directory and afterwards uploaded to AWS S3. 
    
To make it work for your S3 bucket make sure to provide a proper bucket name in Utils.
    
     
## Why 'I Was Framed - Out'
    
'I Was Framed' because I love awful puns and this tool extracts frames. 'Out' because there's also a second tool and I lack imagination. 
    
    
## Technologies used

- JavaFX for GUI stuff;
- Xuggler for video processing;
- Amazon AWS stuff for uploading to... **gasp** AWS S3

