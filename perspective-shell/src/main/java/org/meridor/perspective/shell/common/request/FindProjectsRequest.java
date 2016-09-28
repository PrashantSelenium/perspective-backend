package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Pattern;
import org.meridor.perspective.shell.common.validator.annotation.SupportedCloud;
import org.meridor.perspective.sql.JoinClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.common.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.common.validator.Field.PROJECTS;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindProjectsRequest implements Request<Query> {
    
    private Set<String> ids;
    
    @Pattern
    @Filter(PROJECTS)
    private Set<String> names;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public FindProjectsRequest withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }
    
    public FindProjectsRequest withIds(String ids) {
        this.ids = parseEnumeration(ids);
        return this;
    }
    
    public FindProjectsRequest withClouds(String clouds) {
        this.clouds = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
        Optional<Set<String>> ids = Optional.ofNullable(this.ids);
        Optional<Set<String>> names = Optional.ofNullable(this.names);
        Optional<Set<String>> clouds = Optional.ofNullable(this.clouds);
        JoinClause joinClause = new SelectQuery()
                .all()
                .from()
                .table("projects")
                .innerJoin()
                .table("project_quota")
                .on()
                .equal("projects.project_id", "project_quota.project_id");
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (ids.isPresent()) {
            whereMap.put("id", ids.get());
        }
        if (names.isPresent()) {
            whereMap.put("name", names.get());
        }
        if (clouds.isPresent()) {
            whereMap.put("cloud_type", clouds.get());
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("name")
                        .getQuery();
    }

}
