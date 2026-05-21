package com.inmosmart.interfaces;

import com.inmosmart.model.Inmueble;
import com.inmosmart.model.Oferta;
import com.inmosmart.model.ParametrosBusqueda;

public interface IComprador {
    Oferta construirOferta(Inmueble inmueble, double valorOferta);
    void registrarBusqueda(ParametrosBusqueda params);
    double calcularAhorro(Oferta oferta);
}
