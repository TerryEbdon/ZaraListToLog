package net.ebdon.zaralisttolog

/**
@file
@author Terry Ebdon
@brief Can't use System.console().readLine() with GroovyServ. Code based on Trk21's UI
*/
class UiBase {
  private final Scanner sc = new Scanner(System.in)

  String getLine( final String prompt ) {
    printf prompt
    sc.useDelimiter('\n')
    try {
      sc.next() - '\r' //.toLowerCase()
    } catch ( NoSuchElementException ex ) { // Caused by user entering CTRL+Z
      ''
    }
  }

  void outln( String str ) {
    println str
  }

  void out( String str ) {
    print str
  }
}
