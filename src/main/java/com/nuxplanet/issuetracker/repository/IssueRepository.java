package com.nuxplanet.issuetracker.repository;

import com.nuxplanet.issuetracker.domain.Issue;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Issue entity.
 */
@SuppressWarnings("unused")
public interface IssueRepository extends JpaRepository<Issue,Long> {

    @Query("select issue from Issue issue where issue.originator.login = ?#{principal.username}")
    List<Issue> findByOriginatorIsCurrentUser();

    @Query("select issue from Issue issue where issue.assignee.login = ?#{principal.username}")
    List<Issue> findByAssigneeIsCurrentUser();

}
