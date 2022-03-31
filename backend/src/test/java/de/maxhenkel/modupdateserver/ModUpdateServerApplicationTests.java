package de.maxhenkel.modupdateserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModUpdateServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testWebAuth() throws Exception {
        this.mockMvc.perform(get("/")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testMods() throws Exception {
        this.mockMvc.perform(get("/mods")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testUpdates() throws Exception {
        this.mockMvc.perform(get("/updates/gravestone")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testAddMod() throws Exception {
        this.mockMvc
                .perform(
                        post("/mods/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "modID": "examplemod",
                                          "name": "Example Mod",
                                          "description": "Just an example mod",
                                          "websiteURL": "https://example.com/examplemod",
                                          "downloadURL": "https://example.com/examplemod/files",
                                          "issueURL": "https://example.com/examplemod/issues"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testApiKeys() throws Exception {
        this.mockMvc
                .perform(get("/apikeys"))
                .andExpect(status().isUnauthorized());
    }

}
