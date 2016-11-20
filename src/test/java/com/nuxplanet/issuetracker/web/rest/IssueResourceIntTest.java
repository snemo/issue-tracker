package com.nuxplanet.issuetracker.web.rest;

import com.nuxplanet.issuetracker.IssueTrackerApp;

import com.nuxplanet.issuetracker.domain.Issue;
import com.nuxplanet.issuetracker.repository.IssueRepository;
import com.nuxplanet.issuetracker.service.IssueService;
import com.nuxplanet.issuetracker.repository.search.IssueSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nuxplanet.issuetracker.domain.enumeration.State;
import com.nuxplanet.issuetracker.domain.enumeration.Priority;
/**
 * Test class for the IssueResource REST controller.
 *
 * @see IssueResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IssueTrackerApp.class)
public class IssueResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final LocalDate DEFAULT_CREATED = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED = LocalDate.now(ZoneId.systemDefault());

    private static final State DEFAULT_STATE = State.OPEN;
    private static final State UPDATED_STATE = State.INPROGRESS;

    private static final Priority DEFAULT_PRIORITY = Priority.MINOR;
    private static final Priority UPDATED_PRIORITY = Priority.NORMAL;

    private static final byte[] DEFAULT_ATTACHMENT = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_ATTACHMENT = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_ATTACHMENT_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_ATTACHMENT_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_COMMENT = "AAAAA";
    private static final String UPDATED_COMMENT = "BBBBB";

    @Inject
    private IssueRepository issueRepository;

    @Inject
    private IssueService issueService;

    @Inject
    private IssueSearchRepository issueSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restIssueMockMvc;

    private Issue issue;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        IssueResource issueResource = new IssueResource();
        ReflectionTestUtils.setField(issueResource, "issueService", issueService);
        this.restIssueMockMvc = MockMvcBuilders.standaloneSetup(issueResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Issue createEntity(EntityManager em) {
        Issue issue = new Issue()
                .name(DEFAULT_NAME)
                .description(DEFAULT_DESCRIPTION)
                .created(DEFAULT_CREATED)
                .state(DEFAULT_STATE)
                .priority(DEFAULT_PRIORITY)
                .attachment(DEFAULT_ATTACHMENT)
                .attachmentContentType(DEFAULT_ATTACHMENT_CONTENT_TYPE)
                .comment(DEFAULT_COMMENT);
        return issue;
    }

    @Before
    public void initTest() {
        issueSearchRepository.deleteAll();
        issue = createEntity(em);
    }

    @Test
    @Transactional
    public void createIssue() throws Exception {
        int databaseSizeBeforeCreate = issueRepository.findAll().size();

        // Create the Issue

        restIssueMockMvc.perform(post("/api/issues")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(issue)))
                .andExpect(status().isCreated());

        // Validate the Issue in the database
        List<Issue> issues = issueRepository.findAll();
        assertThat(issues).hasSize(databaseSizeBeforeCreate + 1);
        Issue testIssue = issues.get(issues.size() - 1);
        assertThat(testIssue.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testIssue.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testIssue.getCreated()).isEqualTo(DEFAULT_CREATED);
        assertThat(testIssue.getState()).isEqualTo(DEFAULT_STATE);
        assertThat(testIssue.getPriority()).isEqualTo(DEFAULT_PRIORITY);
        assertThat(testIssue.getAttachment()).isEqualTo(DEFAULT_ATTACHMENT);
        assertThat(testIssue.getAttachmentContentType()).isEqualTo(DEFAULT_ATTACHMENT_CONTENT_TYPE);
        assertThat(testIssue.getComment()).isEqualTo(DEFAULT_COMMENT);

        // Validate the Issue in ElasticSearch
        Issue issueEs = issueSearchRepository.findOne(testIssue.getId());
        assertThat(issueEs).isEqualToComparingFieldByField(testIssue);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = issueRepository.findAll().size();
        // set the field null
        issue.setName(null);

        // Create the Issue, which fails.

        restIssueMockMvc.perform(post("/api/issues")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(issue)))
                .andExpect(status().isBadRequest());

        List<Issue> issues = issueRepository.findAll();
        assertThat(issues).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = issueRepository.findAll().size();
        // set the field null
        issue.setDescription(null);

        // Create the Issue, which fails.

        restIssueMockMvc.perform(post("/api/issues")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(issue)))
                .andExpect(status().isBadRequest());

        List<Issue> issues = issueRepository.findAll();
        assertThat(issues).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreatedIsRequired() throws Exception {
        int databaseSizeBeforeTest = issueRepository.findAll().size();
        // set the field null
        issue.setCreated(null);

        // Create the Issue, which fails.

        restIssueMockMvc.perform(post("/api/issues")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(issue)))
                .andExpect(status().isBadRequest());

        List<Issue> issues = issueRepository.findAll();
        assertThat(issues).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllIssues() throws Exception {
        // Initialize the database
        issueRepository.saveAndFlush(issue);

        // Get all the issues
        restIssueMockMvc.perform(get("/api/issues?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(issue.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
                .andExpect(jsonPath("$.[*].created").value(hasItem(DEFAULT_CREATED.toString())))
                .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
                .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
                .andExpect(jsonPath("$.[*].attachmentContentType").value(hasItem(DEFAULT_ATTACHMENT_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].attachment").value(hasItem(Base64Utils.encodeToString(DEFAULT_ATTACHMENT))))
                .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())));
    }

    @Test
    @Transactional
    public void getIssue() throws Exception {
        // Initialize the database
        issueRepository.saveAndFlush(issue);

        // Get the issue
        restIssueMockMvc.perform(get("/api/issues/{id}", issue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(issue.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.created").value(DEFAULT_CREATED.toString()))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.attachmentContentType").value(DEFAULT_ATTACHMENT_CONTENT_TYPE))
            .andExpect(jsonPath("$.attachment").value(Base64Utils.encodeToString(DEFAULT_ATTACHMENT)))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingIssue() throws Exception {
        // Get the issue
        restIssueMockMvc.perform(get("/api/issues/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateIssue() throws Exception {
        // Initialize the database
        issueService.save(issue);

        int databaseSizeBeforeUpdate = issueRepository.findAll().size();

        // Update the issue
        Issue updatedIssue = issueRepository.findOne(issue.getId());
        updatedIssue
                .name(UPDATED_NAME)
                .description(UPDATED_DESCRIPTION)
                .created(UPDATED_CREATED)
                .state(UPDATED_STATE)
                .priority(UPDATED_PRIORITY)
                .attachment(UPDATED_ATTACHMENT)
                .attachmentContentType(UPDATED_ATTACHMENT_CONTENT_TYPE)
                .comment(UPDATED_COMMENT);

        restIssueMockMvc.perform(put("/api/issues")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedIssue)))
                .andExpect(status().isOk());

        // Validate the Issue in the database
        List<Issue> issues = issueRepository.findAll();
        assertThat(issues).hasSize(databaseSizeBeforeUpdate);
        Issue testIssue = issues.get(issues.size() - 1);
        assertThat(testIssue.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testIssue.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testIssue.getCreated()).isEqualTo(UPDATED_CREATED);
        assertThat(testIssue.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testIssue.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testIssue.getAttachment()).isEqualTo(UPDATED_ATTACHMENT);
        assertThat(testIssue.getAttachmentContentType()).isEqualTo(UPDATED_ATTACHMENT_CONTENT_TYPE);
        assertThat(testIssue.getComment()).isEqualTo(UPDATED_COMMENT);

        // Validate the Issue in ElasticSearch
        Issue issueEs = issueSearchRepository.findOne(testIssue.getId());
        assertThat(issueEs).isEqualToComparingFieldByField(testIssue);
    }

    @Test
    @Transactional
    public void deleteIssue() throws Exception {
        // Initialize the database
        issueService.save(issue);

        int databaseSizeBeforeDelete = issueRepository.findAll().size();

        // Get the issue
        restIssueMockMvc.perform(delete("/api/issues/{id}", issue.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean issueExistsInEs = issueSearchRepository.exists(issue.getId());
        assertThat(issueExistsInEs).isFalse();

        // Validate the database is empty
        List<Issue> issues = issueRepository.findAll();
        assertThat(issues).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchIssue() throws Exception {
        // Initialize the database
        issueService.save(issue);

        // Search the issue
        restIssueMockMvc.perform(get("/api/_search/issues?query=id:" + issue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(issue.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].created").value(hasItem(DEFAULT_CREATED.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].attachmentContentType").value(hasItem(DEFAULT_ATTACHMENT_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].attachment").value(hasItem(Base64Utils.encodeToString(DEFAULT_ATTACHMENT))))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())));
    }
}
