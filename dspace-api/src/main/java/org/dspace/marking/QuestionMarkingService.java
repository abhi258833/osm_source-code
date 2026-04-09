/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.marking;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Question-wise Marking & Annotation Service for On-Screen Marking (OSM) Project
 * DSpace 9.2 Compatible
 * 
 * @author DSpace Developer
 */
@Service
public class QuestionMarkingService {

    @Autowired
    private ItemService itemService;

    /**
     * Save marks and annotation for a question
     * @throws AuthorizeException 
     */
    public void addQuestionMark(Context context, Item item, int questionNo,
                                double marksObtained, double totalMarks, String annotation)
            throws SQLException, AuthorizeException {

        String qStr = String.valueOf(questionNo);

        // Clear old data for this question
        itemService.clearMetadata(context, item, "local", "marking", "questionNo", qStr);
        itemService.clearMetadata(context, item, "local", "marking", "marksObtained", Item.ANY);
        itemService.clearMetadata(context, item, "local", "marking", "totalMarks", Item.ANY);
        itemService.clearMetadata(context, item, "local", "marking", "annotation", Item.ANY);

        // Add new values
        itemService.addMetadata(context, item, "local", "marking", "questionNo", null, qStr);
        itemService.addMetadata(context, item, "local", "marking", "marksObtained", null, String.valueOf(marksObtained));
        itemService.addMetadata(context, item, "local", "marking", "totalMarks", null, String.valueOf(totalMarks));
        itemService.addMetadata(context, item, "local", "marking", "annotation", null,
                annotation == null || annotation.trim().isEmpty() ? "No comment" : annotation.trim());

        itemService.update(context, item);
        context.commit();
    }

    /**
     * Get all question-wise marking data for an Item
     */
    public List<MetadataValue> getQuestionMarkingData(Item item) {
        return itemService.getMetadata(item, "local", "marking", Item.ANY, Item.ANY);
    }

    /**
     * Calculate total marks obtained across all questions
     */
    public double calculateTotalMarksObtained(Item item) {
        List<MetadataValue> marksList = itemService.getMetadata(item, "local", "marking", "marksObtained", Item.ANY);
        return marksList.stream()
                .mapToDouble(mv -> {
                    try {
                        return Double.parseDouble(mv.getValue());
                    } catch (Exception e) {
                        return 0.0;
                    }
                })
                .sum();
    }

    /**
     * Get marks and annotation for a specific question
     */
    public String getQuestionDetails(Item item, int questionNo) {
        List<MetadataValue> qList = itemService.getMetadata(item, "local", "marking", "questionNo", null);
        for (MetadataValue mv : qList) {
            if (mv.getValue().equals(String.valueOf(questionNo))) {
                String marks = getFirstMetadata(item, "marksObtained");
                String total = getFirstMetadata(item, "totalMarks");
                String anno = getFirstMetadata(item, "annotation");
                return String.format("Q%d: %s/%s | Annotation: %s", questionNo, marks, total, anno);
            }
        }
        return "Question " + questionNo + " not marked yet.";
    }

    private String getFirstMetadata(Item item, String qualifier) {
        List<MetadataValue> list = itemService.getMetadata(item, "local", "marking", qualifier, Item.ANY);
        return list.isEmpty() ? "" : list.get(0).getValue();
    }

    /**
     * Get all question marking metadata
     */
    public List<MetadataValue> getAllQuestionMarks(Item item) {
        return itemService.getMetadata(item, "local", "marking", Item.ANY, Item.ANY);
    }
}