package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ProjectsTableFetcher extends BaseTableFetcher<Project> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<Project> getBeanClass() {
        return Project.class;
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.PROJECTS;
    }

    @Override
    protected Collection<Project> getRawData() {
        return projectsAware.getProjects();
    }
}
