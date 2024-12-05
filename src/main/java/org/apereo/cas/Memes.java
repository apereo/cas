package org.apereo.cas;

import java.security.SecureRandom;

/**
 * This is {@link Memes}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class Memes {
    private final String[] urls;

    public static final Memes MAINTENANCE_POLICY = new Memes(
        "https://github.com/apereo/cas/assets/1205228/49a290aa-b358-4ed0-ab51-195f7706128e",
        "https://github.com/apereo/cas/assets/1205228/65f9f9dd-6948-41d6-a668-db8d359dd0cd",
        "https://github.com/apereo/cas/assets/1205228/96024002-514f-448d-a4b4-846d97121b30",
        "https://github.com/apereo/cas/assets/1205228/7ce68cf6-aff0-4125-9787-35e41db61535",
        "https://github.com/apereo/cas/assets/1205228/17a3dd7e-e656-4627-b607-4293e8aaa12f",
        "https://github.com/apereo/cas/assets/1205228/3d00382d-6e32-4578-b8ad-5e33ba813e64",
        "https://github.com/apereo/cas/assets/1205228/65d485fb-f3ec-4f46-8270-1dca2a95816e"
    );
    
    public static final Memes NO_TESTS = new Memes(
        "https://github.com/mmoayyed/cas/assets/1205228/8b8b8515-acce-4830-8a82-922442e4acad",
        "https://github.com/apereo/cas/assets/1205228/42a2e3c6-316a-41b3-96a0-2cc6fdb60f57",
        "https://github.com/apereo/cas/assets/1205228/1c8e3d1e-1c03-4dbe-b4b5-10f9592d7714",
        "https://github.com/apereo/cas/assets/1205228/8a633d54-e7cf-4ebf-a404-a7353ec79956",
        "https://github.com/apereo/cas/assets/1205228/6d9e849b-4f66-4e2d-8f33-d69525750399",
        "https://github.com/apereo/cas/assets/1205228/6fb85fdf-2081-4040-ae01-b7074b6505c6",
        "https://github.com/apereo/cas/assets/1205228/b2f788da-9fd9-4520-be44-22e4c37e39c2",
        "https://github.com/apereo/cas/assets/1205228/54e6511a-6a50-404d-9872-d131f9b9f841",
        "https://github.com/apereo/cas/assets/1205228/c8897bdf-536e-41c4-a8da-e62156956735",
        "https://github.com/apereo/cas/assets/1205228/7ce68cf6-aff0-4125-9787-35e41db61535",
        "https://github.com/apereo/cas/assets/1205228/9b641bc1-4baa-4d7a-9336-3fad57826f2c",
        "https://github.com/apereo/cas/assets/1205228/324584ba-5209-4759-aee0-2fa1643e44eb",
        "https://github.com/apereo/cas/assets/1205228/c4bf44c3-5942-4fff-972c-8091028329fa",
        "https://github.com/apereo/cas/assets/1205228/51205a1c-4181-4bb1-9978-48e2d63696c2",
        "https://github.com/apereo/cas/assets/1205228/afd5e1d2-4bd0-4ad8-8a81-5634e2fb5f3b",
        "https://github.com/apereo/cas/assets/1205228/ba6d58c8-9c73-4e72-a44d-9901cfba7755",
        "https://github.com/apereo/cas/assets/1205228/a99efe36-fa5f-4c06-a712-fe5d9a3d5c59",
        "https://github.com/apereo/cas/assets/1205228/2da2e230-f5d6-40bc-9eb9-633ed10b282e",
        "https://github.com/apereo/cas/assets/1205228/c2bf494f-9e0b-4cb0-98f3-ceeeca4a873f",
        "https://github.com/apereo/cas/assets/1205228/b0374110-f0a3-4792-aca6-e4c55f5f00e8",
        "https://github.com/apereo/cas/assets/1205228/e87faae4-3781-49c5-95ab-fe592ba0d777"
    );

    public static final Memes PULL_REQUEST_APPROVED = new Memes(
        "https://github.com/user-attachments/assets/d9bdc4d0-745c-4a8b-8214-ec1699bc2170",
        "https://github.com/apereo/cas/assets/1205228/1dc37fac-56fa-440f-9e08-0af714d2727d",
        "https://github.com/apereo/cas/assets/1205228/dbc4bf79-af4c-48a2-8037-c0466d9d4eb5",
        "https://github.com/apereo/cas/assets/1205228/228d990f-e048-40bf-b850-379e5af36009",
        "https://github.com/user-attachments/assets/30c6180c-05ba-494f-9173-e5e5f519bf13",
        "https://github.com/apereo/cas/assets/1205228/b8f7f21b-bde6-44fe-9391-312b6a8f39be",
        "https://github.com/apereo/cas/assets/1205228/b7a8c97c-5299-41f5-9965-6e9ce2b4e2a7",
        "https://github.com/apereo/cas/assets/1205228/45422e03-188d-46e4-b1a3-d4aacbed2160",
        "https://github.com/user-attachments/assets/406cadfe-67d0-423c-bffd-e22f52ec2257",
        "https://github.com/apereo/cas/assets/1205228/347d7dd7-6f97-48ad-90e2-a737827f737e",
        "https://github.com/user-attachments/assets/53032ae4-4612-4a42-9385-56b6ef44ae8e",
        "https://github.com/user-attachments/assets/ebca8e40-16a7-45c5-b2bb-5c075e5f42db",
        "https://github.com/apereo/cas/assets/1205228/b5a423b1-f63d-4bd3-b2a8-1eb1e5d1c1ec",
        "https://github.com/apereo/cas/assets/1205228/dec1826c-0495-4227-96ec-a8c239f5e5a7",
        "https://github.com/apereo/cas/assets/1205228/c490c182-a798-4a69-8a9b-61b5c51dbc06",
        "https://github.com/apereo/cas/assets/1205228/218e2cb4-1169-4bfe-8b5c-98211f59aa95",
        "https://github.com/apereo/cas/assets/1205228/e7fa72f8-8647-417e-ac8e-91631803d9da",
        "https://github.com/apereo/cas/assets/1205228/c54f0c47-b92c-4fa0-813e-4b7a1f2714ef",
        "https://github.com/apereo/cas/assets/1205228/647aa7fe-dd73-4355-8b34-03d957d13b3a",
        "https://github.com/apereo/cas/assets/1205228/7d81516e-0579-495c-8a04-49e901dfd7d5",
        "https://github.com/apereo/cas/assets/1205228/36d31a88-f8a6-4595-a93d-50ed69491a65",
        "https://github.com/apereo/cas/assets/1205228/cae9f7b3-86ec-4366-8d7f-cfd1f127007f",
        "https://github.com/apereo/cas/assets/1205228/e9c38624-e474-4c70-b4d5-b8ccc98537a6",
        "https://github.com/apereo/cas/assets/1205228/08dd7c5a-6ee6-4cb9-850a-14ff999e5123",
        "https://github.com/apereo/cas/assets/1205228/62f368b6-eb01-408e-963f-5021e266b4e6",
        "https://github.com/apereo/cas/assets/1205228/70ab09b2-edc8-4f9f-abda-4a4558e2ead0",
        "https://github.com/apereo/cas/assets/1205228/730395f6-26bf-4ee2-a536-3ec8e5cbd37d",
        "https://github.com/apereo/cas/assets/1205228/c2fec809-7b8c-4748-b349-661c1c6d2f2b",
        "https://github.com/apereo/cas/assets/1205228/d6a7c714-c2c4-4c81-8218-0169aa8bfc5c",
        "https://github.com/apereo/cas/assets/1205228/92be5b5e-7bf6-4485-a1ca-9479d271ab46",
        "https://github.com/apereo/cas/assets/1205228/4ef59720-36cb-461a-8cfd-d08f9ed54613",
        "https://github.com/apereo/cas/assets/1205228/2a18efb5-c53e-4f91-bdaf-c38d57d0e485",
        "https://github.com/apereo/cas/assets/1205228/9481aa6c-1cd3-4bc9-8a6d-ca98f2bfdc1d",
        "https://github.com/apereo/cas/assets/1205228/ffa2d297-9095-4602-b38a-5844530ef017",
        "https://github.com/apereo/cas/assets/1205228/865dee51-9fc6-473d-a68b-f06d60757124"
    );

    Memes(final String... urls) {
        this.urls = urls;
    }

    public String select() {
        var random = new SecureRandom();
        var randomIndex = random.nextInt(urls.length);
        return urls[randomIndex];
    }
}
