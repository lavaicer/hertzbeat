/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hertzbeat.manager.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.hertzbeat.common.entity.dto.Message;
import org.apache.hertzbeat.common.entity.dto.PluginUpload;
import org.apache.hertzbeat.common.entity.manager.PluginMetadata;
import org.apache.hertzbeat.manager.service.PluginService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * plugin management API
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "Plugin Manage API")
@RestController
@RequestMapping(path = "/api/plugin", produces = {APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class PluginController {

    private final PluginService pluginService;

    @PostMapping
    @Operation(summary = "upload plugin", description = "upload plugin")
    public ResponseEntity<Message<Void>> uploadNewPlugin(@Valid PluginUpload pluginUpload) {
        pluginService.savePlugin(pluginUpload);
        return ResponseEntity.ok(Message.success("Add success"));
    }


    @GetMapping()
    @Operation(summary = "Get Plugins information", description = "Obtain plugins information based on conditions")
    public ResponseEntity<Message<Page<PluginMetadata>>> getPlugins(
        @Parameter(description = "plugin name search", example = "status") @RequestParam(required = false) String search,
        @Parameter(description = "List current page", example = "0") @RequestParam(defaultValue = "0") int pageIndex,
        @Parameter(description = "Number of list pagination", example = "8") @RequestParam(defaultValue = "8") int pageSize) {
        // Get tag information
        Specification<PluginMetadata> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> andList = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                Predicate predicateApp = criteriaBuilder.like(root.get("name"), "%" + search + "%");
                andList.add(predicateApp);
            }
            Predicate[] andPredicates = new Predicate[andList.size()];
            Predicate andPredicate = criteriaBuilder.and(andList.toArray(andPredicates));

            if (andPredicates.length == 0) {
                return query.where().getRestriction();
            } else {
                return andPredicate;
            }
        };
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
        Page<PluginMetadata> alertPage = pluginService.getPlugins(specification, pageRequest);
        Message<Page<PluginMetadata>> message = Message.success(alertPage);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping()
    @Operation(summary = "Delete plugins based on ID", description = "Delete plugins based on ID")
    public ResponseEntity<Message<Void>> deleteTags(
        @Parameter(description = "Plugin IDs ", example = "6565463543") @RequestParam(required = false) List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            pluginService.deletePlugins(new HashSet<>(ids));
        }
        return ResponseEntity.ok(Message.success("Delete success"));
    }


    @PutMapping()
    @Operation(summary = "Update enable status", description = "Delete plugins based on ID")
    public ResponseEntity<Message<Void>> updatePluginStatus(@RequestBody PluginMetadata plugin) {
        pluginService.updateStatus(plugin);
        return ResponseEntity.ok(Message.success("Update success"));
    }
}
