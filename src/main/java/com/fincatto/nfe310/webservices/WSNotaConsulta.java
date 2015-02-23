package com.fincatto.nfe310.webservices;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import com.fincatto.nfe310.NFeConfig;
import com.fincatto.nfe310.classes.NFAutorizador31;
import com.fincatto.nfe310.classes.nota.consulta.NFNotaConsulta;
import com.fincatto.nfe310.classes.nota.consulta.NFNotaConsultaRetorno;
import com.fincatto.nfe310.parsers.NotaFiscalChaveParser;
import com.fincatto.nfe310.transformers.NFRegistryMatcher;
import com.fincatto.nfe310.webservices.gerado.NfeConsulta2Stub;
import com.fincatto.nfe310.webservices.gerado.NfeConsulta2Stub.NfeConsultaNF2Result;

class WSNotaConsulta {
    private final NFeConfig config;
    private final static Logger log = Logger.getLogger(WSNotaConsulta.class);

    public WSNotaConsulta(final NFeConfig config) {
        this.config = config;
    }

    public NFNotaConsultaRetorno consultaNota(final String chaveDeAcesso) throws Exception {
        final OMElement omElementConsulta = AXIOMUtil.stringToOM(this.gerarDadosConsulta(chaveDeAcesso).toString());
        WSNotaConsulta.log.info(omElementConsulta);

        final OMElement omElementRetorno = this.efetuaConsulta(omElementConsulta, new NotaFiscalChaveParser(chaveDeAcesso));
        WSNotaConsulta.log.info(omElementRetorno);
        return new Persister(new NFRegistryMatcher(), new Format(0)).read(NFNotaConsultaRetorno.class, omElementRetorno.toString());
    }

    private OMElement efetuaConsulta(final OMElement omElementConsulta, final NotaFiscalChaveParser notaFiscalChaveParser) throws AxisFault, RemoteException {
        final NfeConsulta2Stub.NfeCabecMsg cabec = new NfeConsulta2Stub.NfeCabecMsg();
        cabec.setCUF(notaFiscalChaveParser.getNFUnidadeFederativa().getCodigoIbge());
        cabec.setVersaoDados("3.10");

        final NfeConsulta2Stub.NfeCabecMsgE cabecE = new NfeConsulta2Stub.NfeCabecMsgE();
        cabecE.setNfeCabecMsg(cabec);

        final NfeConsulta2Stub.NfeDadosMsg dados = new NfeConsulta2Stub.NfeDadosMsg();
        dados.setExtraElement(omElementConsulta);
        final NfeConsultaNF2Result consultaNF2Result = new NfeConsulta2Stub(NFAutorizador31.valueOfCodigoUF(this.config.getCUF()).getNfeConsultaProtocolo(this.config.getAmbiente())).nfeConsultaNF2(dados, cabecE);
        return consultaNF2Result.getExtraElement();
    }

    private NFNotaConsulta gerarDadosConsulta(final String chaveDeAcesso) {
        final NFNotaConsulta notaConsulta = new NFNotaConsulta();
        notaConsulta.setAmbiente(this.config.getAmbiente());
        notaConsulta.setChave(chaveDeAcesso);
        notaConsulta.setServico("CONSULTAR");
        notaConsulta.setVersao(new BigDecimal("3.10"));
        return notaConsulta;
    }
}