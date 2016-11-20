package com.nuxplanet.issuetracker.service;

import com.nuxplanet.issuetracker.domain.Issue;
import com.nuxplanet.issuetracker.repository.IssueRepository;
import com.nuxplanet.issuetracker.repository.search.IssueSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Issue.
 */
@Service
@Transactional
public class IssueService {

    private final Logger log = LoggerFactory.getLogger(IssueService.class);
    
    @Inject
    private IssueRepository issueRepository;

    @Inject
    private IssueSearchRepository issueSearchRepository;

    /**
     * Save a issue.
     *
     * @param issue the entity to save
     * @return the persisted entity
     */
    public Issue save(Issue issue) {
        log.debug("Request to save Issue : {}", issue);
        Issue result = issueRepository.save(issue);
        issueSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the issues.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<Issue> findAll(Pageable pageable) {
        log.debug("Request to get all Issues");
        Page<Issue> result = issueRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one issue by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Issue findOne(Long id) {
        log.debug("Request to get Issue : {}", id);
        Issue issue = issueRepository.findOne(id);
        return issue;
    }

    /**
     *  Delete the  issue by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Issue : {}", id);
        issueRepository.delete(id);
        issueSearchRepository.delete(id);
    }

    /**
     * Search for the issue corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Issue> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Issues for query {}", query);
        Page<Issue> result = issueSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }
}
