package socket;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Socket Unit Tests")
@SelectPackages({ "socket.client", "socket.server", "socket.stream" })
@IncludeClassNamePatterns(".*TestGroup")
public class _JavaSocketTestSuite {

}
