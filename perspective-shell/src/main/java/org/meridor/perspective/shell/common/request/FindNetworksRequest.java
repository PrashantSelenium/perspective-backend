package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Pattern;
import org.meridor.perspective.shell.common.validator.annotation.SupportedCloud;
import org.meridor.perspective.sql.JoinClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.common.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindNetworksRequest implements Request<Query> {
    
    @Pattern
    @Filter(NETWORK_NAMES)
    private Set<String> name;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> project;

    public FindNetworksRequest withNames(String name) {
        this.name = parseEnumeration(name);
        return this;
    }

    public FindNetworksRequest withProjects(String projects) {
        this.project = parseEnumeration(projects);
        return this;
    }

    public FindNetworksRequest withClouds(String clouds) {
        this.cloud = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
        JoinClause joinClause = new SelectQuery()
                .columns(
                        "networks.id",
                        "networks.name",
                        "projects.name",
                        "networks.state",
                        "networks.is_shared",
                        "network_subnets.cidr"
                )
                .from()
                .table("networks")
                .innerJoin()
                    .table("projects")
                    .on()
                    .equal("networks.project_id", "projects.id")
                .innerJoin()
                    .table("network_subnets")
                    .on()
                    .and(new HashMap<String, String>(){
                        {
                            put("network_subnets.project_id", "projects.id");
                            put("network_subnets.network_id", "networks.id");
                        }
                    });
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (name != null) {
            whereMap.put("networks.name", name);
        }
        if (cloud != null) {
            whereMap.put("projects.cloud_type", cloud);
        }
        if (project != null) {
            whereMap.put("projects.name", project);
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("networks.name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("networks.name")
                        .getQuery();
    }

}
