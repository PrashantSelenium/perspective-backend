package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.query.ShowNetworksQuery;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProjectsRepositoryImpl implements ProjectsRepository {
    
    @Autowired
    private ApiProvider apiProvider;
    
    @Override public List<Project> showProjects(ShowProjectsQuery query) {
        GenericType<ArrayList<Project>> projectListType = new GenericType<ArrayList<Project>>() {};
        List<Project> allProjects = apiProvider.getProjectsApi().getAsXml(projectListType);
        return allProjects.stream().filter(query.getPayload()).collect(Collectors.toList());
    }
    
    @Override public List<Flavor> showFlavors(String projectNames, String clouds, ShowFlavorsQuery showFlavorsQuery) {
        ShowProjectsQuery showProjectsQuery = new ShowProjectsQuery().withNames(projectNames).withClouds(clouds);
        List<Project> projects = showProjects(showProjectsQuery);
        return projects.stream()
                .flatMap(p -> p.getFlavors().stream())
                .filter(showFlavorsQuery.getPayload())
                .collect(Collectors.toList());
    }
    
    @Override public List<Network> showNetworks(String projectNames, String clouds, ShowNetworksQuery showNetworksQuery) {
        ShowProjectsQuery showProjectsQuery = new ShowProjectsQuery().withNames(projectNames).withClouds(clouds);
        List<Project> projects = showProjects(showProjectsQuery);
        return projects.stream()
                .flatMap(p -> p.getNetworks().stream())
                .filter(showNetworksQuery.getPayload())
                .collect(Collectors.toList());
    }

}
