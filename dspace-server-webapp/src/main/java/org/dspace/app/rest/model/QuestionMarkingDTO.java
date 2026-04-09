/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

public class QuestionMarkingDTO {

    private int questionNo;
    private double marksObtained;
    private double totalMarks;
    private String annotation;

    // Constructors
    public QuestionMarkingDTO() {}

    // Getters & Setters
    public int getQuestionNo() { return questionNo; }
    public void setQuestionNo(int questionNo) { this.questionNo = questionNo; }

    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }

    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }

    public String getAnnotation() { return annotation; }
    public void setAnnotation(String annotation) { this.annotation = annotation; }
}