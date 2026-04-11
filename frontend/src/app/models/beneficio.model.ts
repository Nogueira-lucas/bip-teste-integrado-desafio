export interface Beneficio {
  id?: number;
  nome: string;
  descricao?: string;
  valor: number;
  ativo: boolean;
  version?: number;
}

export interface TransferenciaRequest {
  fromId: number;
  toId: number;
  valor: number;
}
