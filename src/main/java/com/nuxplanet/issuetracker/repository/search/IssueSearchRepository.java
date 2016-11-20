package com.nuxplanet.issuetracker.repository.search;

import com.nuxplanet.issuetracker.domain.Issue;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Issue entity.
 */
public interface IssueSearchRepository extends ElasticsearchRepository<Issue, Long> {
}
