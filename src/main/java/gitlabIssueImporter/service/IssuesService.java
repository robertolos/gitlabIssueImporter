package gitlabIssueImporter.service;

import gitlabIssueImporter.model.Issue;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabIssue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by roberto on 08/05/17.
 */
public class IssuesService {
    public static List<Issue> readFromSpreadsheet(String fileName){
        List<Issue> issues = new LinkedList<Issue>();

        FileInputStream excelFile = null;
        try {
            excelFile = new FileInputStream(new File(fileName));
            Workbook workbook = new HSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext()) {

                Row row = iterator.next();
                if(row.getRowNum() == 0){
                    continue;
                }
                String projectId = getCellValue(row.getCell(12));
                String assigneeId = getCellValue(row.getCell(10));
                String milestoneId = getCellValue(row.getCell(15));
                String labels = getCellValue(row.getCell(1));
                String description = getCellValue(row.getCell(17));
                String title = getCellValue(row.getCell(2));

                Issue issue = new Issue();
                issue.setProjectId(Integer.parseInt(projectId));
                issue.setAssigneeId(Integer.parseInt(assigneeId));
                if(StringUtils.isNotEmpty(milestoneId)){
                    issue.setMilestoneId(Integer.parseInt(milestoneId));
                }
                issue.setLabels(labels);
                issue.setDescription(description);
                issue.setTitle(title);
                issues.add(issue);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(excelFile);
        }

        return issues;
    }

    public static void createIssues(final GitlabAPI api, final List<Issue> issues){
        System.out.println("Starting creation of " + (issues!=null ? issues.size() : 0) + " issues... Please wait.");
        for (Issue issue : issues) {
            try {
                GitlabIssue result = api.createIssue(issue.getProjectId(),
                        issue.getAssigneeId(),
                        issue.getMilestoneId(),
                        issue.getLabels(),
                        issue.getDescription(),
                        issue.getTitle());
                System.out.println("Issue created. ID:" + result.getId());
            } catch (IOException e) {
                System.out.println("error: " + e);
                System.out.println(e.getStackTrace());
            }
        }

    }

    private static String getCellValue(Cell cell) {
        if(cell == null){
            return "";
        }
        CellType cellType = cell.getCellTypeEnum();
        String result ="";
        switch (cellType) {

            case NUMERIC: // numeric value in Excel
            case FORMULA: // precomputed value based on formula
                result = ""+ String.valueOf((int)cell.getNumericCellValue());
                break;
            case STRING: // String Value in Excel
                result = cell.getStringCellValue();
                break;
            case BLANK:
                result = "";
            case BOOLEAN: //boolean value
                result: cell.getBooleanCellValue();
                break;
            case ERROR:
            default:
                System.out.println("There is no support for this type of cell");
        }
        return result.trim();
    }
}
