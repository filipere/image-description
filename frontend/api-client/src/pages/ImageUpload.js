import React, { useState } from "react";
import api from '../services/api';
import './ImageUploader.css';

const ImageUploader = () => {
    const [file, setFile] = useState(null);
    const [description, setDescription] = useState("");
    const [loading, setLoading] = useState(false);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setDescription("");
    };

    const handleUpload = async () => {
        if (!file) return;

        const formData = new FormData();
        formData.append('image', file); // deve ser 'image' para compatibilidade com o backend

        try {
            setLoading(true);
            const response = await api.post('upload-images', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            setDescription(response.data.description || "Descrição não encontrada.");
        } catch (error) {
            console.error("Erro ao processar imagem", error);
            setDescription("Erro ao processar a imagem.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <h2>Descrever Imagem com IA</h2>
            <div className="file-input">
                <input type="file" accept="image/*" onChange={handleFileChange} />
            </div>
            <button className="upload-button" onClick={handleUpload} disabled={!file || loading}>
                {loading ? 'Enviando...' : 'Upload e descreva'}
            </button>
            <div className="description-result">
                <h2>Descrição</h2>
                <p>{description}</p>
            </div>
        </div>
    );
};

export default ImageUploader;
