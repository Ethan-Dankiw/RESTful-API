package socket.client;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Client Socket")
@SelectPackages("socket.client")
@IncludeClassNamePatterns(".*Test")
public class _SocketClientTestGroup {

}
