[![Dependency review](https://github.com/TerryEbdon/ZaraListToLog/actions/workflows/dependency-review.yml/badge.svg)](https://github.com/TerryEbdon/ZaraListToLog/actions/workflows/dependency-review.yml)

# ZaraListToLog

ZaraListToLog takes a ZaraRadio playlist and turns it into a ZaraRadio log file.
ZaraListToLog can create the log for any date and start time.
This is useful to:

- Create a log file for a date when a radio show is to be repeated. e.g. when
  you're playing a recorded show but still want the logs. 
- Recreate a log file that has been lost or deleted.
- Create a log for a show that had logging disabled. (ZaraRadio doesn't log by
  default, and its configuration isn't always persistent.)

## Prerequisites

### Java

ZaraListToLog depends on Java, which must be on the path.
ZaraListToLog releases target Java 17, or later. While it should be possible
to build for Java 8 and 11, this is not currently tested.

### Operating System

ZaraListToLog should run on any operating system that supports
Java 17 or later. (Possibly Java 8 or later, see above.) Currently I'm only
testing on Windows. Let me know if you have a non-Windows
requirement.

### Microsoft Windows

ZaraRadio is a Windows only application. It writes Windows file paths ino its
playlists and log files. ZaraListToLog is tested on Windows 10 and Windows 11,
with 64-bit Intel processors. While it should be possible to run the app on
Windows 7, on 32-bit machines, and with ARM processors, I don't plan to test on
those platforms. Let me know if you've tried any of those combinations.
