class CasDiscoveryProfile {
    static profile = null;
    static loadingPromise = null;

    static fromProfile(profileJson) {
        const instance = new CasDiscoveryProfile();
        Object.assign(instance, profileJson);
        return instance;
    }

    static fetchIfNeeded() {
        // Already loaded
        if (this.profile) {
            return $.Deferred().resolve(this.profile).promise();
        }

        // Request in flight → return same promise
        if (this.loadingPromise) {
            return this.loadingPromise;
        }

        if (!actuatorEndpoints.discoveryprofile) {
            return $.Deferred()
                .reject("No discovery profile endpoint")
                .promise();
        }

        const deferred = $.Deferred();
        this.loadingPromise = deferred.promise();

        $.get(actuatorEndpoints.discoveryprofile)
            .done(response => {
                this.profile = CasDiscoveryProfile.fromProfile(response.profile);
                deferred.resolve(this.profile);
            })
            .fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                deferred.reject(xhr);
            })
            .always(() => {
                this.loadingPromise = null;
            });

        return this.loadingPromise;
    }

    static availableAttributes() {
        return this.profile?.availableAttributes ?? [];
    }

    static multifactorAuthenticationProviders() {
        return this.profile?.multifactorAuthenticationProviderTypesSupported ?? [];
    }
}
