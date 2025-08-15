package net.ebdon.zaralisttolog

import java.sql.Timestamp
import java.util.concurrent.TimeUnit
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JFrame

@groovy.util.logging.Log4j2('logger')
class ZaraListToLog extends UiBase {
  final int numPlaylistHeaderLines   = 1
  final int expectNumFieldsInLogLine = 2
  final int versionBorderPadding     = 2
  final String unplayableLength      = '-1'

  final String overlapRegex  = /.*~[0-9]+([\.][0-9]*){0,1}\s*\.(wav|mp3|ogg|wma)/
  final String fileTypeRegex = /\s|.mp3|.ogg|.wma|.wav/

  // final String playListName = 'Thursday GM Show 29 August 2024.lst'
  String logFileName

  int playlistFileLineNo = 0
  int unplayableCount    = 0
  int overlappedTracks   = 0
  int totalOverlapMs     = 0

  File logFile
  File playlistFile
  String showStart         = '2024-05-22 09:00:00'
  Timestamp timestamp      = Timestamp.valueOf(showStart)
  Timestamp startTimeStamp = timestamp.clone()

  static void main(String[] args) {
    new ZaraListToLog().run()
  }

  void run() {
    reportVersion()
    selectPlayList()
    selectShowStart()
    reportFiles()

    logFile = new File(logFileName )
    if (logFile.exists()) {
      logger.error 'Log file already exists'
    } else {
      createLogHeader()

      // playlistFile = new File(playListName)
      if (playlistFile.exists()) {
        processPlayList()
        trackSummary()
      } else {
        log.error 'Playlist does not exist'
      }
    }
    outln '\n'
  }

  void selectPlayList() {
    JFileChooser playListDialogue =
      new JFileChooser(
        dialogTitle: 'Choose a ZaraRadio playlist',
        fileSelectionMode: JFileChooser.FILES_ONLY,
        //the file filter must show also directories, in order to be able to look into them
        fileFilter: [
          getDescription: { -> '*.lst' },
          accept: { file -> file ==~ /.*?\.lst/ || file.directory },
        ] as FileFilter
      )

    playListDialogue.showOpenDialog()
    logger.trace "Selected file: ${playListDialogue.selectedFile}"
    playlistFile = playListDialogue?.selectedFile.absoluteFile
    if ( playlistFile.exists() ) {
      logFileName = playlistFile.absolutePath - '.lst' + '.log'
    }
  }

  void selectShowStart() {
    Closure popup = { String prompt
      JFrame jframe = new JFrame()
      String answer = JOptionPane.showInputDialog(jframe, prompt, showStart)
      jframe.dispose()
      answer
    }

    showStart = popup('Start date and time for the show')

    timestamp      = Timestamp.valueOf(showStart)
    startTimeStamp = timestamp.clone()
  }

  void trackSummary() {
    outln '\nFound:'
    outln String.format('%,14d playable tracks', playedTrackCount)
    outln String.format('%,14d unplayable tracks', unplayableCount)
    outln String.format('%,14d tracks were overlapped', overlappedTracks)
    outln String.format('%,14d mS total overlap', totalOverlapMs)
    outln String.format( '%14s Zara runtime', totalRuntime)

    outln '\nUnplayable count includes stop commands and tracks with bad lengths'
  }

  int getPlayedTrackCount() {
     playlistFileLineNo - unplayableCount - numPlaylistHeaderLines
  }

  final String getTotalRuntime() {
    final long playListMs = playlistRunTimeMs

    String.format('%d min, %d sec',
        TimeUnit.MILLISECONDS.toMinutes(playListMs),
        TimeUnit.MILLISECONDS.toSeconds(playListMs) -
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playListMs))
    )
  }

  final Long getPlaylistRunTimeMs() {
    timestamp.time - startTimeStamp.time
  }

  void reportFiles() {
    logger.info "Reading ${playlistFile.absolutePath}"
    logger.info "Writing $logFileName"
    outln ''
  }

  void processPlayList() {
    playlistFile.eachLine { line ->
      if ( playlistFileLineNo++ != 0 ) {
        List<String> fields = line.split( '\t' )
        assert fields.size() == expectNumFieldsInLogLine
        if ( fields.first() != unplayableLength ) {
          String logTime = timestamp.toString()[11..18]
          final String trackPath = fields.last()
          logFile << "$logTime\tstart\t${trackPath}\n"
          add( fields.first(), overlapMilliseconds(fields) )
        } else {
          ++unplayableCount
        }
      } else {
        outln String.format('%4s tracks expected in playlist', line)
      }
    }
  }

  final long overlapMilliseconds(final List<String> fields) {
    final long millsPerSecond = 1000
    final long notOverlapped  = 0

    long trackOverlap = notOverlapped
    logger.trace "path: ${fields.last()}"
    // Using File.pathSeparator to avoid a Java internal error
    final String trackName = fields.last().split(File.pathSeparator).last()

    final Boolean overlapped = (trackName.toLowerCase() ==~ overlapRegex)

    if (overlapped) {
      ++overlappedTracks
      final String overlapSeconds =
          (trackName.split('~').last().replaceAll(fileTypeRegex,''))
      logger.trace "Overlap: >$overlapSeconds< mS"
      trackOverlap = overlapSeconds.toFloat() * millsPerSecond
      totalOverlapMs += trackOverlap
    }
    trackOverlap
  }

  void createLogHeader() {
    logFile << 'LOG FILE\n'
    logFile << '========\n\n'
    logFile << 'Wednesday 22 May 2024\n\n'
    logFile << 'TIME    \tACTION\n'
    logFile << '--------\t------\n'
  }

  private void reportVersion() {
    final Package myPackage  = this.class.package
    final String title       = myPackage.implementationTitle
    final String version     = myPackage.implementationVersion

    final String format      = '│ %s %s │'
    final String versionInfo = String.format format, title, version
    logger.debug versionInfo[1..-1]

    final Integer hLineLen      = versionInfo.size() - versionBorderPadding
    final String  hLine         = '─' * hLineLen
    final String  versionHeader = '┌' + hLine + '┐'
    final String  versionFooter = '└' + hLine + '┘'

    newLine()
    outln versionHeader
    outln versionInfo
    outln versionFooter
    newLine()
  }

  void newLine() {
    out '\n'
  }
  void add( final String durationMs, long overlap) {
    int milliseconds = durationMs.toLong() - overlap
    Calendar cal = Calendar.instance
    cal.timeInMillis = timestamp.time
    cal.add(Calendar.MILLISECOND, milliseconds)
    timestamp = new Timestamp(cal.time.time)
  }
}
