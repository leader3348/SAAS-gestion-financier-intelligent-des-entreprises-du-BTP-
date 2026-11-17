import { mad, STATUS_LABELS } from './format'
describe('format', () => {
  it('formats MAD amounts', () => { expect(mad(1250.5)).toMatch(/1[\s.\u202f\u00a0]?250,50/); expect(mad(0)).toMatch(/0,00/) })
  it('labels every invoice status in French', () => { for (const s of ['DRAFT','ISSUED','PARTIALLY_PAID','PAID','OVERDUE','CANCELLED']) expect(STATUS_LABELS[s]).toBeTruthy() })
})
