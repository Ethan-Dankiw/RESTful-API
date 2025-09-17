package socket.stream.integration;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite

@SuiteDisplayName("Input/Output Stream Integration")
@SelectPackages("socket.stream.integration")
@IncludeClassNamePatterns(".*Test")
public class _StreamIntegrationTestType {

}
