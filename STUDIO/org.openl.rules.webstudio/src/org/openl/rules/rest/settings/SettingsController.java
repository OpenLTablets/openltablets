package org.openl.rules.rest.settings;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.openl.rules.rest.settings.model.EntrypointSettingsModel;
import org.openl.rules.rest.settings.model.SettingsModel;
import org.openl.rules.rest.settings.model.SupportedFeaturesModel;
import org.openl.rules.rest.settings.model.UserManagementMode;
import org.openl.rules.security.AdminPrivilege;
import org.openl.rules.webstudio.web.admin.AdministrationSettings;
import org.openl.rules.webstudio.web.admin.SettingsService;

@RestController
@Validated
@RequestMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Settings")
public class SettingsController {

    private final Environment environment;
    private final SettingsService settingsService;
    private final Optional<Supplier<String>> logoutUrlProvider;
    private final BooleanSupplier mailSenderFeature;

    public SettingsController(Environment environment,
                              SettingsService settingsService,
                              @Qualifier("logoutUrlProvider") Optional<Supplier<String>> logoutUrlProvider,
                              @Qualifier("mailSenderFeature") BooleanSupplier mailSenderFeature) {
        this.environment = environment;
        this.settingsService = settingsService;
        this.logoutUrlProvider = logoutUrlProvider;
        this.mailSenderFeature = mailSenderFeature;
    }

    @Operation(summary = "msg.get-app-settings.summary", description = "msg.get-app-settings.desc")
    @GetMapping
    public SettingsModel getSettings() {
        var userManagementMode = getUserManagementMode();
        return SettingsModel.builder()
                .entrypoint(EntrypointSettingsModel.builder()
                        .logoutUrl(logoutUrlProvider.map(Supplier::get).orElse(null))
                        .build())
                .userMode(userManagementMode)
                .supportedFeatures(SupportedFeaturesModel.builder()
                        .groupsManagement(userManagementMode == UserManagementMode.EXTERNAL)
                        .userManagement(userManagementMode != null)
                        .emailVerification(mailSenderFeature.getAsBoolean())
                        .build())
                .build();
    }

    private UserManagementMode getUserManagementMode() {
        return switch (environment.getProperty("user.mode")) {
            case null -> null;
            case "single" -> null;
            case "demo", "multi" -> UserManagementMode.INTERNAL;
            default -> UserManagementMode.EXTERNAL;
        };
    }

    @AdminPrivilege
    @GetMapping("/system")
    public AdministrationSettings getAdministrationSettings() {
        var settings = new AdministrationSettings();
        settingsService.load(settings);
        return settings;
    }

    @AdminPrivilege
    @PostMapping("/system")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveAdministrationSettings(@Valid @RequestBody AdministrationSettings settings) throws IOException {
        settingsService.store(settings);
        settingsService.commit();
    }

}
