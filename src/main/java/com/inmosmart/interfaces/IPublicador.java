package com.inmosmart.interfaces;

import com.inmosmart.model.Inmueble;
import com.inmosmart.model.Oferta;

public interface IPublicador {
    void publicarInmueble(Inmueble inmueble);
    void gestionarOferta(Oferta oferta, boolean aceptar);
    double calcularGanancias();
}
