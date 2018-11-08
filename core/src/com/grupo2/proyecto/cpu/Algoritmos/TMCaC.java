/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grupo2.proyecto.cpu.Algoritmos;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.grupo2.proyecto.cpu.util.ActorProc;
import com.grupo2.proyecto.cpu.util.ProcComparator;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Luis
 */
public class TMCaC implements AlgoCPU {

    public float a;
    public int e0;
    int conmut;
    int conmutActual;
    public ArrayList<Proceso> procesos;
    public ArrayList<Proceso> cola;
    int porTerminar;
    public Proceso activo;
    public int t;
    private ArrayList<ActorProc> aProcs;
    Skin skin;
    ProcComparator comparator;
    int cContexto;

    public TMCaC(ArrayList<Proceso> procesos, int conmut, Skin skin, float a, int e0) {
        this.conmut = conmut;
        this.skin = skin;
        this.a = a;
        this.e0 = e0;
        Collections.reverse(procesos);
        this.procesos = procesos;
        this.cola = new ArrayList<Proceso>(6);
        this.aProcs = new ArrayList<ActorProc>(6);
        porTerminar = procesos.size();
        comparator = new ProcComparator();
    }

    @Override
    public boolean simular() {
        for (ActorProc aProc : aProcs) {
            if (!aProc.getProceso().equals(activo)) {
                aProc.desactivar();
            }
        }
        if (porTerminar == 0) {
            for (ActorProc aProc : aProcs) {
                aProc.desactivar();
            }
            return true;
        }
        if (conmutActual != 0) {
            for (Proceso proceso : cola) {
                proceso.addtRespuesta();
                proceso.addtEspera();
            }
            conmutActual -= 1;
            t++;
            return false;
        }
        nuevoProcListo();
        if (activo != null) {
            aProcs.get(procesos.indexOf(activo)).activar();
        }
        if (usar()) {
            nuevaEstimacion(activo);
            verSiTermino(activo);
            siguienteProceso();
        }
        t++;
        for (Proceso proceso : cola) {
            proceso.addtRespuesta();
            if (!proceso.equals(activo)) {
                proceso.addtEspera();
            }
            proceso.addtRetorno();
        }
        for (ActorProc aProc : aProcs) {
            aProc.actualizar();
        }
        return false;
    }

    private boolean usar() {
        if (activo == null) {
            return false;
        }
        return activo.usarCPU();
    }

    int aux;

    private void siguienteProceso() {
        if (cola.isEmpty()) {
            return;
        }
        aux = activo.id;
        cola.sort(comparator);
        activo = cola.get(0);
        activo.esperando = false;
        if (aux != activo.id) {
            cContexto += 1;
        }
        conmutActual = conmut;
    }

    private void nuevoProcListo() {
        for (Proceso proceso : procesos) {
            if (proceso.listoEn == t) {
                proceso.en = e0;
                cola.add(proceso);
                cola.sort(comparator);
                aProcs.add(new ActorProc(skin, proceso, procesos.indexOf(proceso) + 1));
            }
            if (activo == null) {
                activo = cola.get(0);
                aProcs.get(cola.indexOf(proceso)).activar();
            }
        }
    }

    private void verSiTermino(Proceso proceso) {
        if (proceso.terminado) {
            porTerminar -= 1;
            aProcs.get(procesos.indexOf(activo)).terminar();
            cola.remove(proceso);
        }
    }

    private void nuevaEstimacion(Proceso proceso) {
        proceso.en = (((float) proceso.en) * a + ((float) proceso.tAnte) * (1 - a));
    }

    @Override
    public ArrayList<ActorProc> getAProcs() {
        return aProcs;
    }

    @Override
    public int getT() {
        return t;
    }

    @Override
    public float getPromedioRespuesta() {
        float total = 0;
        for (Proceso proceso : procesos) {
            total += proceso.tRespuesta;
        }
        return total / procesos.size();
    }

    @Override
    public float getPromedioEspera() {
        float total = 0;
        for (Proceso proceso : procesos) {
            total += proceso.tEspera;
        }
        return total / procesos.size();
    }

    @Override
    public float getPromedioRetorno() {
        float total = 0;
        for (Proceso proceso : procesos) {
            total += proceso.tRetorno;
        }
        return total / procesos.size();
    }

    @Override
    public int getCContexto() {
        return cContexto;
    }

}
