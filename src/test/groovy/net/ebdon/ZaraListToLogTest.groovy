package net.ebdon.zaralisttolog

import groovy.test.GroovyTestCase
import java.sql.Timestamp

/**
 * Unit tests for the ZaraListToLog functionality.
 *
 * <p>This test class exercises and verifies the behaviour of class
 * ZaraListToLog.
 *
 * @author Terry Ebdon
 * @version 1.0
 * @since 1.0
 * @see net.ebdon.ZaraListToLog
 */
class ZaraListToLogTest extends GroovyTestCase {
  private ZaraListToLog app
  private final String day                = '22'
  private final String year               = '2024'
  private final String playDate           = "$year-05-$day"
  private final String startPlayTimeStamp = "$playDate 19:30:00"
  private final String endPlayTimeStamp   = "$playDate 20:00:00"

  void setUp() {
    super.setUp()
    app = new ZaraListToLog()
  }

  void testInitTimestampsParsesShowStart() {
    app.with {
      showStart = startPlayTimeStamp
      initTimestamps()
      assert timestamp == Timestamp.valueOf(startPlayTimeStamp) &&
        startTimeStamp == timestamp
    }
  }

  void testExtractLogDateFormatsHeaderDate() {
    app.showStart = startPlayTimeStamp
    app.extractLogDate()
    assert app.logHeaderDate == "Wednesday $day May $year"
  }

  void testAddAdvancesTimestampByDurationMinusOverlap() {
    app.timestamp = Timestamp.valueOf(startPlayTimeStamp)
    app.startTimeStamp = app.timestamp.clone()
    app.add('10000', 2500) // 10,000ms - 2,500ms = 7,500ms
    long expected = Timestamp.valueOf(startPlayTimeStamp).time + 7500
    assert app.timestamp.time == expected
  }

  void testOverlapMillisecondsWhenNoOverlapReturnsZero() {
    final List<String> fields = ['123', 'song.mp3']
    long overlap = app.overlapMilliseconds(fields)
    assert overlap == 0
  }

  void testOverlapMillisecondsParsesOverlapSeconds() {
    final List<String> fields = ['123', 'C:\\music\\track~1.5.mp3']
    long overlap = app.overlapMilliseconds(fields)
    assert overlap == 1500 &&
      app.overlappedTracks == 1 &&
      app.totalOverlapMs == 1500
  }

  void testPlayedTrackCountAndRuntime() {
    final long numHdrLines = 1
    app.with {
      playlistFileLineNo = 12
      unplayableCount    = 2
      startTimeStamp     = Timestamp.valueOf(startPlayTimeStamp)
      timestamp          = Timestamp.valueOf(endPlayTimeStamp)

      long expectNumPlayed = playlistFileLineNo - numHdrLines - unplayableCount
      assert playedTrackCount == expectNumPlayed
      assert totalRuntime == '30 min, 0 sec'
    }
  }
}
