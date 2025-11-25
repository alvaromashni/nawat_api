package br.com.smartmesquitaapi.pix.infrastructure;

import br.com.smartmesquitaapi.pix.exception.QrCodeGenerationException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Gerador de imagens QR Code a partir de payloads PIX
 * Utiliza a biblioteca ZXing (Zebra Crossing)
 */
public class QrcodeImageGenerator {

    // Configurações padrão do QR Code
    private static final int DEFAULT_SIZE = 300; // Tamanho em pixels
    private static final int MIN_SIZE = 150;
    private static final int MAX_SIZE = 1000;

    /**
     * Gera uma imagem QR Code em Base64 a partir do payload PIX
     *
     * @param payload Payload EMV do PIX
     * @return Imagem PNG em Base64
     * @throws QrCodeGenerationException se houver erro na geração
     */
    public static String generateBase64(String payload){
        return generateBase64(payload, DEFAULT_SIZE);
    }

    /**
     * Gera uma imagem QR Code em Base64 com tamanho customizado
     *
     * @param payload Payload EMV do PIX
     * @param size Tamanho da imagem em pixels (largura e altura)
     * @return Imagem PNG em Base64
     * @throws QrCodeGenerationException se houver erro na geração
     */
    public static String generateBase64(String payload, int size){
        validateInput(payload, size);

        try {
            byte[] imageBytes = generatePngBytes(payload, size);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch(Exception e) {
            throw new QrCodeGenerationException("Erro ao gerar QR code: " + e.getMessage(), e);
        }
    }
    /**
     * Gera os bytes da imagem PNG do QR Code
     *
     * @param payload Payload EMV do PIX
     * @param size Tamanho da imagem em píxeis
     * @return Array de bytes da imagem PNG
     * @throws WriterException se houver erro na geração do QR
     * @throws IOException se houver erro na conversão para PNG
     */
    public static byte[] generatePngBytes(String payload, int size) throws WriterException, IOException {

        // Cria o QR code usando Zxing
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                payload,
                BarcodeFormat.QR_CODE,
                size,
                size
        );

        // converte a imagem pra png
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Gera uma imagem QR Code em Base64 com configurações otimizadas para mobile
     * (tamanho médio, boa qualidade)
     *
     * @param payload Payload EMV do PIX
     * @return Imagem PNG em Base64
     */
    public static String generateForMobile(String payload){
        return generateBase64(payload, 250);
    }

    /**
     * Gera uma imagem QR Code em Base64 com alta qualidade
     * (tamanho maior, ideal para impressão)
     *
     * @param payload Payload EMV do PIX
     * @return Imagem PNG em Base64
     */
    public static String generateHighQuality(String payload){
        return generateBase64(payload, 500);
    }

    private static void validateInput(String payload, int size) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload não pode ser vazio");
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Tamanho deve estar entre %d e %d pixels", MIN_SIZE, MAX_SIZE)
            );
        }
    }
}
