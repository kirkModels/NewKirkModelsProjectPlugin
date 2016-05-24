package ianmann.kirkmodels.newkirkmodelsproject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.NewExampleAction;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import iansLibrary.utilities.JSONUtils;

public class KirkModelsProjectWizard extends Wizard implements INewWizard {
	
	private final String WIZARD_NAME = "New KirkModels Project";
	private IWorkbench workbench;
	
	/**
	 * Name of the project. path to project will be the path
	 * to the workspace with projectName appended on to it.
	 */
	private String projectName;
	private IWorkspaceRoot root;
	private IProject basicProject;
	private IJavaProject javaProject;
	
	private String settingsFileName = "settings.json";
	private String mainClassName = "MainClass";
	
	private WizardNewProjectCreationPage _pageOne;

	public KirkModelsProjectWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.workbench = workbench;
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		try {
			this.createBaseProject();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public void addPages() {
	    super.addPages();
	 
	    _pageOne = new WizardNewProjectCreationPage("Main Class");
	    _pageOne.setTitle("Main Class");
	    _pageOne.setDescription("Main class For the project");
	    
	    addPage(_pageOne);
	}
	
	/**
	 * I got the instructions on how to do this here:
	 * {@link https://sdqweb.ipd.kit.edu/wiki/JDT_Tutorial:_Creating_Eclipse_Java_Projects_Programmatically }
	 * @throws CoreException
	 */
	private void createBaseProject() throws CoreException{
		initializeProject();
		
		setLibraries();
		
		IFolder sourceFolder = setScr();
		
		addMainClass(sourceFolder);
		
		this.createSettingsFile();
		
		this.createMigrationFolder();
	}

	private void initializeProject() throws CoreException {
		this.projectName = this._pageOne.getProjectName();
		this.root = ResourcesPlugin.getWorkspace().getRoot();
		this.basicProject = root.getProject(this.projectName);
		
		this.basicProject.create(null);
		this.basicProject.open(null);
		
		IProjectDescription description = this.basicProject.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		this.basicProject.setDescription(description, null);
		
		this.javaProject = JavaCore.create(this.basicProject);
	}

	private void addMainClass(IFolder sourceFolder) throws JavaModelException, CoreException {
		String mainPackage = this.projectName + ".application";
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(mainPackage, false, null);
		
		//get string for class declaration
		String source = Utils.getClassDeclaration(mainPackage, this.mainClassName, this.settingsFileName);
		
		IFile mainClass = this.basicProject.getFile("src/" + mainPackage.replace(".", "/") + "/" + this.mainClassName + ".java");
		mainClass.create(new ByteArrayInputStream(source.getBytes()), false, null);
	}

	private IFolder setScr() throws CoreException, JavaModelException {
		IFolder sourceFolder = this.basicProject.getFolder("src");
		sourceFolder.create(false, true, null);
		
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(sourceFolder);
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
		this.javaProject.setRawClasspath(newEntries, null);
		return sourceFolder;
	}

	private void setLibraries() throws CoreException, JavaModelException {
		IFolder binFolder = this.basicProject.getFolder("bin");
		binFolder.create(false, true, null);
		this.javaProject.setOutputLocation(binFolder.getFullPath(), null);
		
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		for (LibraryLocation element : locations) {
		 entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		}
		//add libs to project class path
		this.javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
	}
	
	private void createSettingsFile() throws CoreException {
		IFolder settingsFolder = this.basicProject.getFolder("settings");
		settingsFolder.create(false, true, null);
		
		String source = JSONUtils.formatJSON(Utils.settingsTemplate(this.projectName), 0);
		
		IFile settingsFile = this.basicProject.getFile(settingsFolder.getFullPath().toString() + "/" + this.settingsFileName);
		settingsFile.create(new ByteArrayInputStream(source.getBytes()), false, null);
	}
	
	private void createMigrationFolder() throws CoreException {
		IFolder migrationsFolder = this.basicProject.getFolder("migrations");
		migrationsFolder.create(false, true, null);
	}

}
