appender("FILE", FileAppender) {
	file = "server.log"
	append = true
	encoder(PatternLayoutEncoder) {
	  pattern = "%level %logger - %msg%n"
	}
  }
  
  root(DEBUG, ["FILE"])