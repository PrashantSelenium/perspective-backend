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
import static org.meridor.perspective.shell.common.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindKeypairsRequest implements Request<Query> {
    
    @Pattern
    @Filter(KEYPAIR_NAMES)
    private Set<String> name;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> project;

    public FindKeypairsRequest withNames(String name) {
        this.name = parseEnumeration(name);
        return this;
    }

    public FindKeypairsRequest withProjects(String projects) {
        this.project = parseEnumeration(projects);
        return this;
    }

    public FindKeypairsRequest withClouds(String clouds) {
        this.cloud = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
        Optional<Set<String>> names = Optional.ofNullable(this.name);
        Optional<Set<String>> clouds = Optional.ofNullable(this.cloud);
        Optional<Set<String>> projects = Optional.ofNullable(this.project);
        JoinClause joinClause = new SelectQuery()
                .columns(
                        "keypairs.name",
                        "keypairs.fingerprint",
                        "projects.name"
                )
                .from()
                .table("keypairs")
                .innerJoin()
                .table("projects")
                .on()
                .equal("keypairs.project_id", "projects.id");
        Map<String, Collection<String>> whereMap = new HashMap<>();

        if (names.isPresent()) {
            whereMap.put("keypairs.name", names.get());
        }
        if (clouds.isPresent()) {
            whereMap.put("projects.cloud_type", clouds.get());
        }
        if (projects.isPresent()) {
            whereMap.put("projects.name", projects.get());
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("keypairs.name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("keypairs.name")
                        .getQuery();
    }

}
