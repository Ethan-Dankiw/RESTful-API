package parser.file;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("File Parser")
@SelectPackages({ "parser.file" })
@IncludeClassNamePatterns(".*Test")
public class _FileParserTestType {

}
