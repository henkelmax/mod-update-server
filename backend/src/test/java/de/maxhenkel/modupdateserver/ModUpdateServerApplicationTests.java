package de.maxhenkel.modupdateserver;

import de.maxhenkel.modupdateserver.entities.ModEntity;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModUpdateServerApplicationTests {

    private static final UUID API_KEY = new UUID(0L, 0L);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModRepository modRepository;

    @BeforeEach
    public void addExampleData() {
        ModEntity mod = new ModEntity();
        mod.setModID("testmod");
        mod.setName("Example Mod");
        mod.setDescription("Just an example mod");
        mod.setWebsiteURL("https://example.com/examplemod");
        mod.setDownloadURL("https://example.com/examplemod/files");
        mod.setIssueURL("https://example.com/examplemod/issues");
        modRepository.save(mod);
    }

    @Test
    public void testWebAuth() throws Exception {
        this.mockMvc.perform(get("/")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testAddModNoPermission() throws Exception {
        this.mockMvc
                .perform(
                        post("/mods/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(generateMod("examplemod"))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testModAlreadyExists() throws Exception {
        this.mockMvc
                .perform(
                        post("/mods/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(generateMod("testmod"))
                                .header("apikey", API_KEY.toString())
                )
                .andExpect(status().isConflict());
    }

    @Test
    public void testAddMod() throws Exception {
        this.mockMvc
                .perform(
                        post("/mods/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(generateMod("examplemod"))
                                .header("apikey", API_KEY.toString())
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testMods() throws Exception {
        this.mockMvc.perform(get("/mods")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testUpdates() throws Exception {
        this.mockMvc.perform(get("/updates/testmod")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testApiKeys() throws Exception {
        this.mockMvc
                .perform(get("/apikeys"))
                .andExpect(status().isUnauthorized());
    }

    private String generateMod(String modId) {
        return """
                {
                  "modID": "%s",
                  "name": "Example Mod",
                  "description": "Just an example mod",
                  "websiteURL": "https://example.com/examplemod",
                  "downloadURL": "https://example.com/examplemod/files",
                  "issueURL": "https://example.com/examplemod/issues"
                }
                """.formatted(modId);
    }

}
