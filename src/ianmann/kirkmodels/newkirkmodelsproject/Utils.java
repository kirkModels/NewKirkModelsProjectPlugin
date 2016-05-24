package ianmann.kirkmodels.newkirkmodelsproject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

abstract class Utils {

	public static final String mainClassTemplate = "" + 
		"package %s;\n\n" +
			
		"import java.io.FileNotFoundException;\n" +
		"import java.lang.reflect.InvocationTargetException;\n" +
		"import java.sql.SQLException;\n" +
		"import org.json.simple.parser.ParseException;\n\n" +
		
		"import kirkModels.orm.Project;\n" +
		"import kirkModels.orm.backend.sync.DbSync;\n" +
		"import kirkModels.utils.exceptions.ObjectNotFoundException;\n\n" +

		"public class %s {\n\n\t" +
			
			"public static void main(String[] args) throws FileNotFoundException, ParseException, SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ObjectNotFoundException {\n\n\t\t" +
			
				"Project.initialize(\"settings/%s\");\n\n\t" +
				
			"}\n\n" +
			
		"}";
	
	private static JSONObject settingsTemplate = new JSONObject();
	private static boolean settingsTemplateCached = false;
	
	public static String getClassDeclaration(String... info) {
		String classDeclaration = String.format(mainClassTemplate, (String[]) info);
		return classDeclaration;
	}
	
	public static JSONObject settingsTemplate(String rootProjectPath) {
		if (settingsTemplateCached) {
			return settingsTemplate;
		} else {
			settingsTemplate.put("databases", new JSONObject());
			settingsTemplate.put("defaultDb", "");
			settingsTemplate.put("synced_models", new JSONArray());
			settingsTemplate.put("migrations_folder", "migrations/");
			settingsTemplate.put("project_development_root_parent", rootProjectPath);
			settingsTemplate.put("binary_root_parent", rootProjectPath);
			settingsTemplateCached = true;
		}
		return settingsTemplate;
	}
	
}
