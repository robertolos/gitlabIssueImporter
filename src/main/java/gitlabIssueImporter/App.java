package gitlabIssueImporter;

import gitlabIssueImporter.model.Issue;
import gitlabIssueImporter.service.IssuesService;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;

import java.io.IOException;
import java.util.List;

/**
 * GitLab Issue import from spreadsheet
 *
 */
public class App {
    public static final String ACCESS_TOKEN = "Rv_rLshE6CyuaAt34mrt";
    public static final String GITLAB_URL = "https://gitlab.com";

    public static void main(String[] args ) {
        GitlabAPI api = GitlabAPI.connect(GITLAB_URL, ACCESS_TOKEN);
        try {
            System.out.println("Number of projects: "+api.getProjects().size());
            for (GitlabProject gitlabProject : api.getProjects()) {
                System.out.println(gitlabProject.getId() + " " + gitlabProject.getName());
            }
            List<Issue> issues = IssuesService.readFromSpreadsheet("IssuesData.xls");
            IssuesService.createIssues(api, issues);
            System.out.println("Completed!");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
