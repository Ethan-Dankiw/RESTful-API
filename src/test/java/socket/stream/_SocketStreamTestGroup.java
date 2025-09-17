package socket.stream;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Socket Stream")
@SelectPackages("socket.stream")
@IncludeClassNamePatterns(".*TestType")
public class _SocketStreamTestGroup {

}
