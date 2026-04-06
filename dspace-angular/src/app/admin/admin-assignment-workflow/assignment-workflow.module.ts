import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AssignmentWorkflowComponent } from './assignment-workflow.component';

/**
 * Module for the Assignment Workflow Feature
 * 
 * This module provides a step-by-step UI for:
 * 1. Selecting community/subcommunity/collection
 * 2. Selecting users with checkboxes
 * 3. Confirming and assigning items
 */
@NgModule({
  declarations: [
    AssignmentWorkflowComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  exports: [
    AssignmentWorkflowComponent
  ]
})
export class AssignmentWorkflowModule { }
