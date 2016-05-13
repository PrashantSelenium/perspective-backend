package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ProjectImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ProjectImagesTableFetcher extends BaseTableFetcher<ProjectImage> {

    @Autowired
    private ImagesAware imagesAware;

    @Override
    protected Class<ProjectImage> getBeanClass() {
        return ProjectImage.class;
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.PROJECT_IMAGES;
    }

    @Override
    protected Collection<ProjectImage> getRawData() {
        return imagesAware.getImages().stream()
                .flatMap(i ->
                        i.getProjectIds().stream()
                                .map(p -> new ProjectImage(p, i.getId()))
                )
                .collect(Collectors.toList());
    }
}
