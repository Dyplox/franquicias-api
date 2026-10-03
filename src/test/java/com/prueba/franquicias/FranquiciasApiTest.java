package com.prueba.franquicias;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FranquiciasApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void flujoCompleto() throws Exception {
        long franquicia = crear("/franquicias", "{\"nombre\":\"La Esquina\"}");
        long centro = crear("/franquicias/" + franquicia + "/sucursales", "{\"nombre\":\"Centro\"}");
        long norte = crear("/franquicias/" + franquicia + "/sucursales", "{\"nombre\":\"Norte\"}");

        long arepa = crear("/sucursales/" + centro + "/productos", "{\"nombre\":\"Arepa\",\"stock\":10}");
        long pan = crear("/sucursales/" + centro + "/productos", "{\"nombre\":\"Pan\",\"stock\":40}");
        crear("/sucursales/" + norte + "/productos", "{\"nombre\":\"Jugo\",\"stock\":5}");

        mvc.perform(get("/franquicias/" + franquicia + "/mayor-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sucursal").value("Centro"))
                .andExpect(jsonPath("$[0].producto").value("Pan"))
                .andExpect(jsonPath("$[0].stock").value(40))
                .andExpect(jsonPath("$[1].sucursal").value("Norte"))
                .andExpect(jsonPath("$[1].producto").value("Jugo"));

        mvc.perform(put("/productos/" + arepa + "/stock").param("stock", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(99));
        mvc.perform(get("/franquicias/" + franquicia + "/mayor-stock"))
                .andExpect(jsonPath("$[0].producto").value("Arepa"));

        mvc.perform(delete("/productos/" + arepa)).andExpect(status().isNoContent());
        mvc.perform(get("/franquicias/" + franquicia + "/mayor-stock"))
                .andExpect(jsonPath("$[0].producto").value("Pan"));

        mvc.perform(put("/franquicias/" + franquicia + "/nombre").param("nombre", "La Esquina Dorada"))
                .andExpect(jsonPath("$.nombre").value("La Esquina Dorada"));
        mvc.perform(put("/sucursales/" + centro + "/nombre").param("nombre", "Chapinero"))
                .andExpect(jsonPath("$.nombre").value("Chapinero"));
        mvc.perform(put("/productos/" + pan + "/nombre").param("nombre", "Pan de bono"))
                .andExpect(jsonPath("$.nombre").value("Pan de bono"));
    }

    @Test
    void erroresBasicos() throws Exception {
        mvc.perform(get("/franquicias/9999/mayor-stock")).andExpect(status().isNotFound());
        mvc.perform(delete("/productos/9999")).andExpect(status().isNotFound());
        mvc.perform(post("/franquicias/9999/sucursales")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\"}"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/franquicias")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest());
        long franquicia = crear("/franquicias", "{\"nombre\":\"Otra\"}");
        long sucursal = crear("/franquicias/" + franquicia + "/sucursales", "{\"nombre\":\"Sur\"}");
        mvc.perform(post("/sucursales/" + sucursal + "/productos")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Jugo\",\"stock\":-1}"))
                .andExpect(status().isBadRequest());
    }

    private long crear(String url, String cuerpo) throws Exception {
        String respuesta = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("id").asLong();
    }
}
